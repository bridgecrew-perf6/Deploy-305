package com.cerea_p1.spring.jpa.postgresql.model.game;

import java.util.*;

import com.cerea_p1.spring.jpa.postgresql.payload.response.Jugada;
import com.cerea_p1.spring.jpa.postgresql.security.websocket.OneStompSessionHandler;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient; 

public class Partida  extends TimerTask {
    private int nJugadores;
    private List<Jugador> jugadores;
    private List<Carta> baraja;
    private List<Carta> descartes;
    private List<Regla> reglas;
    private EstadoPartidaEnum estado;
    private String id;
    private int tTurno;
    private int index;
    private int sentido;
    // true indica que la partida es privada, false indica que la partida es pública
    private boolean partidaPrivada;
    @Autowired
    private Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Autowired

	@Override
	public void run(){
            System.out.println("HA SONADO LA ALARMA");
            

            WebSocketClient client = new StandardWebSocketClient();

            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            //Jugada(getUltimaCartaJugada(),getJugadores(), getTurno().getNombre());
            StompSessionHandler sessionHandler = new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                //    logger.info("New session established : " + session.getSessionId());
                //    session.subscribe("/topic/messages", this);
                  //  logger.info("Subscribed to /topic/messages");
                    session.send("/topic/jugada"+id, getJugada(getUltimaCartaJugada(), getJugadores(), getTurno().getNombre()));
                    //logger.info("Message sent to websocket server");
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                 //   logger.error("Got an exception", exception);
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Jugada.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                //   Message msg = (Message) payload;
                    Jugada j = (Jugada) payload;
                // logger.info("Received : " + msg.getText() + " from : " + msg.getFrom());
                }

                Jugada getJugada(Carta c, List<Jugador> j, String n){
                // Jugada play = new Jugada(getUltimaCartaJugada(),getJugadores(), getTurno().getNombre());
                    return new Jugada(c,j,n);
                }

				@Override
				public void handleTransportError(StompSession session, Throwable exception) {
					// TODO Auto-generated method stub
					
				}
            };
            HttpPost post = new HttpPost("https://onep1.herokuapp.com/api/auth/signin");

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("username", "admin"));
            urlParameters.add(new BasicNameValuePair("password", "admin123"));
           // urlParameters.add(new BasicNameValuePair("custom", "secret"));

            try {
                StringEntity params = new StringEntity("details={\"name\":\"admin\",\"password\":\"admin123\"} ");
                post.addHeader("content-type", "application/json");
                post.setEntity(params);
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                System.out.println("Excepcion alarma " + e1.getMessage());
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch(Exception e){
                System.out.println("Excepcion alarma " + e.getMessage());
            }
            stompClient.connect("ws://onep1.herokuapp.com", sessionHandler);
            
		}
    };

    public Partida(boolean tipoPartida) {
        jugadores = new ArrayList<Jugador>();
        baraja = barajaInicial();
        descartes = new ArrayList<Carta>();
        reglas = new ArrayList<Regla>();
        partidaPrivada = tipoPartida;
        Carta carta = baraja.get(baraja.size()-1);
        while(carta.getColor() == Color.UNDEFINED){
            Collections.shuffle(baraja);
            carta = baraja.get(baraja.size()-1);
        }
        descartes.add(baraja.get(baraja.size()-1));
        baraja.remove(baraja.size()-1);
        index = 0;
        sentido = 1;
    }

    public Partida(int numJugadores, int tTurno, List<Regla> reglas){
        this.id = UUID.randomUUID().toString();
        jugadores = new ArrayList<Jugador>();
        baraja = barajaInicial();
        descartes = new ArrayList<Carta>();
        this.reglas = reglas;
        this.nJugadores = numJugadores;
        this.tTurno = tTurno;
        this.estado = EstadoPartidaEnum.NEW;
        partidaPrivada = true;
        Carta carta = baraja.get(baraja.size()-1);
        while(carta.getColor() == Color.UNDEFINED){
            Collections.shuffle(baraja);
            carta = baraja.get(baraja.size()-1);
        }
        descartes.add(baraja.get(baraja.size()-1));
        baraja.remove(baraja.size()-1);
        index = 0;
        sentido = 1;
    }

    public void setTipo(boolean tipo){
        partidaPrivada = tipo;
    }

    public boolean getTipo(){
        return this.partidaPrivada;
    }

    /* Costrucción de la baraja inicial */
    private List<Carta> barajaInicial() {
        List<Carta> baraja = new ArrayList<Carta>();
        
        baraja.add(new Carta(Numero.CERO,Color.ROJO));
        baraja.add(new Carta(Numero.CERO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CERO,Color.AZUL));
        baraja.add(new Carta(Numero.CERO,Color.VERDE));

        baraja.add(new Carta(Numero.UNO,Color.ROJO));
        baraja.add(new Carta(Numero.UNO,Color.ROJO));
        baraja.add(new Carta(Numero.UNO,Color.AMARILLO));
        baraja.add(new Carta(Numero.UNO,Color.AMARILLO));
        baraja.add(new Carta(Numero.UNO,Color.AZUL));
        baraja.add(new Carta(Numero.UNO,Color.AZUL));
        baraja.add(new Carta(Numero.UNO,Color.VERDE));
        baraja.add(new Carta(Numero.UNO,Color.VERDE));
        
        baraja.add(new Carta(Numero.DOS,Color.ROJO));
        baraja.add(new Carta(Numero.DOS,Color.ROJO));
        baraja.add(new Carta(Numero.DOS,Color.AMARILLO));
        baraja.add(new Carta(Numero.DOS,Color.AMARILLO));
        baraja.add(new Carta(Numero.DOS,Color.AZUL));
        baraja.add(new Carta(Numero.DOS,Color.AZUL));
        baraja.add(new Carta(Numero.DOS,Color.VERDE));
        baraja.add(new Carta(Numero.DOS,Color.VERDE));

        baraja.add(new Carta(Numero.TRES,Color.ROJO));
        baraja.add(new Carta(Numero.TRES,Color.ROJO));
        baraja.add(new Carta(Numero.TRES,Color.AMARILLO));
        baraja.add(new Carta(Numero.TRES,Color.AMARILLO));
        baraja.add(new Carta(Numero.TRES,Color.AZUL));
        baraja.add(new Carta(Numero.TRES,Color.AZUL));
        baraja.add(new Carta(Numero.TRES,Color.VERDE));
        baraja.add(new Carta(Numero.TRES,Color.VERDE));

        baraja.add(new Carta(Numero.CUATRO,Color.ROJO));
        baraja.add(new Carta(Numero.CUATRO,Color.ROJO));
        baraja.add(new Carta(Numero.CUATRO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CUATRO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CUATRO,Color.AZUL));
        baraja.add(new Carta(Numero.CUATRO,Color.AZUL));
        baraja.add(new Carta(Numero.CUATRO,Color.VERDE));
        baraja.add(new Carta(Numero.CUATRO,Color.VERDE));

        baraja.add(new Carta(Numero.CINCO,Color.ROJO));
        baraja.add(new Carta(Numero.CINCO,Color.ROJO));
        baraja.add(new Carta(Numero.CINCO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CINCO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CINCO,Color.AZUL));
        baraja.add(new Carta(Numero.CINCO,Color.AZUL));
        baraja.add(new Carta(Numero.CINCO,Color.VERDE));
        baraja.add(new Carta(Numero.CINCO,Color.VERDE));
        
        baraja.add(new Carta(Numero.SEIS,Color.ROJO));
        baraja.add(new Carta(Numero.SEIS,Color.ROJO));
        baraja.add(new Carta(Numero.SEIS,Color.AMARILLO));
        baraja.add(new Carta(Numero.SEIS,Color.AMARILLO));
        baraja.add(new Carta(Numero.SEIS,Color.AZUL));
        baraja.add(new Carta(Numero.SEIS,Color.AZUL));
        baraja.add(new Carta(Numero.SEIS,Color.VERDE));
        baraja.add(new Carta(Numero.SEIS,Color.VERDE));

        baraja.add(new Carta(Numero.SIETE,Color.ROJO));
        baraja.add(new Carta(Numero.SIETE,Color.ROJO));
        baraja.add(new Carta(Numero.SIETE,Color.AMARILLO));
        baraja.add(new Carta(Numero.SIETE,Color.AMARILLO));
        baraja.add(new Carta(Numero.SIETE,Color.AZUL));
        baraja.add(new Carta(Numero.SIETE,Color.AZUL));
        baraja.add(new Carta(Numero.SIETE,Color.VERDE));
        baraja.add(new Carta(Numero.SIETE,Color.VERDE));

        baraja.add(new Carta(Numero.OCHO,Color.ROJO));
        baraja.add(new Carta(Numero.OCHO,Color.ROJO));
        baraja.add(new Carta(Numero.OCHO,Color.AMARILLO));
        baraja.add(new Carta(Numero.OCHO,Color.AMARILLO));
        baraja.add(new Carta(Numero.OCHO,Color.AZUL));
        baraja.add(new Carta(Numero.OCHO,Color.AZUL));
        baraja.add(new Carta(Numero.OCHO,Color.VERDE));
        baraja.add(new Carta(Numero.OCHO,Color.VERDE));

        baraja.add(new Carta(Numero.NUEVE,Color.ROJO));
        baraja.add(new Carta(Numero.NUEVE,Color.ROJO));
        baraja.add(new Carta(Numero.NUEVE,Color.AMARILLO));
        baraja.add(new Carta(Numero.NUEVE,Color.AMARILLO));
        baraja.add(new Carta(Numero.NUEVE,Color.AZUL));
        baraja.add(new Carta(Numero.NUEVE,Color.AZUL));
        baraja.add(new Carta(Numero.NUEVE,Color.VERDE));
        baraja.add(new Carta(Numero.NUEVE,Color.VERDE));

        baraja.add(new Carta(Numero.MAS_DOS,Color.ROJO));
        baraja.add(new Carta(Numero.MAS_DOS,Color.ROJO));
        baraja.add(new Carta(Numero.MAS_DOS,Color.AMARILLO));
        baraja.add(new Carta(Numero.MAS_DOS,Color.AMARILLO));
        baraja.add(new Carta(Numero.MAS_DOS,Color.AZUL));
        baraja.add(new Carta(Numero.MAS_DOS,Color.AZUL));
        baraja.add(new Carta(Numero.MAS_DOS,Color.VERDE));
        baraja.add(new Carta(Numero.MAS_DOS,Color.VERDE));

        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.ROJO));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.ROJO));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.AMARILLO));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.AZUL));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.AZUL));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.VERDE));
        baraja.add(new Carta(Numero.CAMBIO_SENTIDO,Color.VERDE));

        baraja.add(new Carta(Numero.BLOQUEO,Color.ROJO));
        baraja.add(new Carta(Numero.BLOQUEO,Color.ROJO));
        baraja.add(new Carta(Numero.BLOQUEO,Color.AMARILLO));
        baraja.add(new Carta(Numero.BLOQUEO,Color.AMARILLO));
        baraja.add(new Carta(Numero.BLOQUEO,Color.AZUL));
        baraja.add(new Carta(Numero.BLOQUEO,Color.AZUL));
        baraja.add(new Carta(Numero.BLOQUEO,Color.VERDE));
        baraja.add(new Carta(Numero.BLOQUEO,Color.VERDE));
        
        baraja.add(new Carta(Numero.CAMBIO_COLOR,Color.UNDEFINED));
        baraja.add(new Carta(Numero.CAMBIO_COLOR,Color.UNDEFINED));
        baraja.add(new Carta(Numero.CAMBIO_COLOR,Color.UNDEFINED));
        baraja.add(new Carta(Numero.CAMBIO_COLOR,Color.UNDEFINED));

        baraja.add(new Carta(Numero.MAS_CUATRO,Color.UNDEFINED));
        baraja.add(new Carta(Numero.MAS_CUATRO,Color.UNDEFINED));
        baraja.add(new Carta(Numero.MAS_CUATRO,Color.UNDEFINED));
        baraja.add(new Carta(Numero.MAS_CUATRO,Color.UNDEFINED));

        //barajar
        Collections.shuffle(baraja);

        return baraja;
    }

    public void barajarDescartes() {
        for (int i=0; i<descartes.size()-1; ++i) {
            baraja.add(descartes.get(i));
            descartes.remove(i);
        }
        Collections.shuffle(baraja);
    }

    public boolean barajaVacia() {
        return baraja.isEmpty();
    }

    public void repartirManos() {
        for(int i=0; i<7; i++) {
            for (Jugador j : jugadores) {
                j.addCarta(baraja.get(baraja.size()-1));
                baraja.remove(baraja.size()-1);
            }
        }
    }

    public Carta getUltimaCartaJugada(){
        return descartes.get(descartes.size()-1);
    }

    public void jugarCarta(Carta c, String nombreJugador) {
        descartes.add(c);
        for (Jugador j : jugadores) {
            if (j.getNombre().equals(nombreJugador)){
                if(c.numero.equals(Numero.CAMBIO_SENTIDO)){
                    cambioSentido();
                }
                j.deleteCarta(c);
            }
        }
    }

    public void siguienteTurno(){
        index = (index+sentido)%nJugadores;
        if(index <  0){
            index = (index + nJugadores) % nJugadores;
        }
    }

    public Jugador getTurno(){
        return jugadores.get(index);
    }

    public List<Carta> robarCartas(String nombreJugador, int n) {
        List<Carta> robadas = new ArrayList<Carta>();

        if(n > baraja.size())
            barajarDescartes();

        for(int i=0; i<n; ++i) {
            robadas.add(baraja.get(baraja.size()-1));
            baraja.remove(baraja.size()-1);
        }

        return robadas;
    }

    public void addJugador(Jugador j) {
        jugadores.add(j);
    }

    public List<Jugador> getJugadores(){
        return jugadores;
    }

    public Jugador getJugador(Jugador j){
        for(Jugador k : jugadores){
            if(k.equals(j)) return k;
        }
        return null;
    }

    public boolean removePlayer(Jugador j){
        if(jugadores.contains(j)){
            jugadores.remove(j);
            return true;
        } else return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id; 
    }

    public EstadoPartidaEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartidaEnum estado) {
        this.estado = estado;
    }

    public boolean playerAlreadyIn(Jugador p){
        return jugadores.contains(p);
    }

    public int getTTurno() {
        return tTurno;
    }

    public void setTTurno(int tTurno) {
        this.tTurno = tTurno;
    }

    public int getNJugadores() {
        return nJugadores;
    }

    public void setNJugadores(int nJugadores) {
        this.nJugadores = nJugadores;
    }

    public List<Regla> getReglas() {
        return reglas;
    }

    public void setReglas(List<Regla> reglas) {
        this.reglas = reglas;
    }

    public void cambioSentido(){
        sentido = - sentido;
    }

	@Override
		public void run(){
            System.out.println("HA SONADO LA ALARMA");
            

            WebSocketClient client = new StandardWebSocketClient();

            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            //Jugada(getUltimaCartaJugada(),getJugadores(), getTurno().getNombre());
            StompSessionHandler sessionHandler = new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                //    logger.info("New session established : " + session.getSessionId());
                //    session.subscribe("/topic/messages", this);
                  //  logger.info("Subscribed to /topic/messages");
                    session.send("/topic/jugada"+id, getJugada(getUltimaCartaJugada(), getJugadores(), getTurno().getNombre()));
                    //logger.info("Message sent to websocket server");
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                 //   logger.error("Got an exception", exception);
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Jugada.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                //   Message msg = (Message) payload;
                    Jugada j = (Jugada) payload;
                // logger.info("Received : " + msg.getText() + " from : " + msg.getFrom());
                }

                Jugada getJugada(Carta c, List<Jugador> j, String n){
                // Jugada play = new Jugada(getUltimaCartaJugada(),getJugadores(), getTurno().getNombre());
                    return new Jugada(c,j,n);
                }

				@Override
				public void handleTransportError(StompSession session, Throwable exception) {
					// TODO Auto-generated method stub
					
				}
            };
            HttpPost post = new HttpPost("https://onep1.herokuapp.com/api/auth/signin");

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("username", "admin"));
            urlParameters.add(new BasicNameValuePair("password", "admin123"));
           // urlParameters.add(new BasicNameValuePair("custom", "secret"));

            try {
                StringEntity params = new StringEntity("details={\"name\":\"admin\",\"password\":\"admin123\"} ");
                post.addHeader("content-type", "application/json");
                post.setEntity(params);
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                System.out.println("Excepcion alarma " + e1.getMessage());
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch(Exception e){
                System.out.println("Excepcion alarma " + e.getMessage());
            }
            stompClient.connect("ws://onep1.herokuapp.com", sessionHandler);
            
		}

    public void startAlarma() {
        TimerTask task = new TimerTask() {
        @Autowired

		@Override
		public void run() {
            System.out.println("HA SONADO LA ALARMA");
            

            WebSocketClient client = new StandardWebSocketClient();
            StompHeaders headers = new StompHeaders();
            headers.add("Authorization","Bearer admin");

            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            StompSessionHandler sessionHandler = new StompSessionHandler() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    session.send("/game/pasarTurno/"+id, getJugada(getUltimaCartaJugada(), getJugadores(), getTurno().getNombre()));
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                }

                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Jugada.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                //   Message msg = (Message) payload;
                    Jugada j = (Jugada) payload;
                // logger.info("Received : " + msg.getText() + " from : " + msg.getFrom());
                }

                Jugada getJugada(Carta c, List<Jugador> j, String n){
                // Jugada play = new Jugada(getUltimaCartaJugada(),getJugadores(), getTurno().getNombre());
                    return new Jugada(c,j,n);
                }

				@Override
				public void handleTransportError(StompSession session, Throwable exception) {
					// TODO Auto-generated method stub
					
				}
            };
            //sessionHandler.setCosas(id, getUltimaCartaJugada(),getJugadores(),getTurno().getNombre());
           // CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost post = new HttpPost("https://onep1.herokuapp.com/api/auth/signin");

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("username", "admin"));
            urlParameters.add(new BasicNameValuePair("password", "admin123"));
           // urlParameters.add(new BasicNameValuePair("custom", "secret"));
           
    

            try {
                StringEntity params = new StringEntity("details={\"name\":\"admin\",\"password\":\"admin123\"} ");
                post.addHeader("content-type", "application/json");
                post.setEntity(params);
               // post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                System.out.println("Excepcion alarma " + e.getMessage());
            }

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post)) {

                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch(Exception e) {
                System.out.println(e.getMessage());
            }

            stompClient.connect("ws://onep1.herokuapp.com/onep1-game", sessionHandler,headers);
            
		}
        
    };
        timer.schedule(task, tTurno*1000);
    }

    public void cancelarAlarma(){
        task.cancel();
    }
}
