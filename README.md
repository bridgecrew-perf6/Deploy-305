# Deploy
Aplicación en el dominio https://onep1.herokuapp.com/

Para la autentificación de usuarios (tanto registro como login) es necesario usar el prefijo (/api/auth)
Para acceder al login (/signin). Es necesario usar LoginRequest (en el paquete com.cerea_p1.spring.jpa.postgresql.payload.request). Devuelve ResponseEntity (en formato JSON) con la información de nombre de usuario, email, pais y puntos.

Para registrar un usuario se usa (/signup). Es necesario usar SignupRequest (en el paquete com.cerea_p1.spring.jpa.postgresql.payload.request) y devuelve un mensaje de ok del formato que se muestra en el ficero com.cerea_p1.spring.jpa.postgresql.payload.response.MessageResponse.

Registro de usuario:

  - Petición POST a https://onep1.herokuapp.com/api/auth/signup
  
  - JSON:
  
        {
          "username": <nombre_de_usuario>,
          "email": <email>,
          "pais": <pais>,
          "password": <contraseña>
        }
      
  - Información adicional:
  
      - username de entre 3 y 255 caracteres
        
      - password de entre 6 y 255 caracteres
        
      - email que cumpla la siguiente ER: .+[@].+[\\.].+ (algo + @ + algo + . + algo). Máximo 255 caracteres
        
      - pais de máximo 255 caracteres

Logear un usuario:

  - Petición POST a https://onep1.herokuapp.com/api/auth/signin

  - JSON:
      
        {
          "username": <nombre_de_usuario>,
          "password": <contraseña>
        }

Crear una partida:
  
  - Petición POST a https://onep1.herokuapp.com/game/create

  - JSON:

        {
            "playername": <nombre_del_usuario>,
            "nplayers": <numero_de_jugadores>,
            "tturn": <tiempo_de_turno>
        }

  - Devuelve: 

        {
          "jugadores": [
            {
              "nombre": <nombre_de_usuario>,
              "cartas": []
            },
          ],
          "id": <id_partida>,
          "tipo": true
        }

    - Si va mal: codigo 4**, y por qué falla

Enviar una petición de amistad
  - Petición POST a https://onep1.herokuapp.com/friends/send/friend-request

  - JSON:

        {
          "username": <nombre_de_usuario>,
          "friendname": <nombre_del_amigo>
        }

  - Devuelve: 
    - Si va bien: codigo 200, Petición de amistad enviada a " + <nombre_del_amigo>
    - Si va mal: codigo 4**, y por qué falla

Ver invitaciones de amistad
  - Petición POST a https://onep1.herokuapp.com/friends/receive/friend-request

  - JSON:

        {
          "username": <nombre_de_usuario>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
            {
              "message": "[ <nombre_del_amigo1>, <nombre_del_amigo2>, ...]"
            }
    - Si va mal: codigo 4**, y por qué falla

Aceptar invitación de amistad
  - Petición POST a https://onep1.herokuapp.com/friends/accept/friend-request

  - JSON:

        {
          "username": <nombre_de_usuario>,
          "friendname": <nombre_del_amigo>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
            {
              "message": "Amigo añadido: <nombre_del_amigo>"
            }
    - Si va mal: codigo 4**, y por qué falla

Ver lista de amigos
  - Petición POST a https://onep1.herokuapp.com/friends/friendsList

  - JSON:

        {
          "username": <nombre_de_usuario>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
          {
              "message": "[ <nombre_del_amigo1>, <nombre_del_amigo2>, ...]"
          }

    - Si va mal: codigo 4**, y por qué falla

Cancelar petición de amistad
  - Petición POST a https://onep1.herokuapp.com/friends/cancel/fiend-request

  - JSON:

        {
          "username": <nombre_de_usuario>,
          "friendname": <nombre_del_amigo>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
          {
            "message": "Petición de amistad cancelada: <nombre_del_amigo>"
          }
    - Si va mal: codigo 4**, y por qué falla

Eliminar amigo
  - Petición POST a https://onep1.herokuapp.com/friends/deleteFriend

  - JSON:

        {
          "username": <nombre_de_usuario>,
          "friendname": <nombre_del_amigo>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
          {
            "message": "Amigo eliminado: <nombre_del_amigo>"
          }
    - Si va mal: codigo 4**, y por qué falla

Ranking de un pais
  - Petición POST a https://onep1.herokuapp.com/ranking/rankingPais

  - JSON:

        {
          "pais": <pais_del_usuario>
        }
  - Devuelve: 
    - Si va bien: codigo 200, 
          {
            "message": "[\"3nsalada2,10\",\"3nsalada,3\",\"3nsalada3,0\"]"
          }
    - Si va mal: codigo 4**, y por qué falla

Ranking de un mundial
  - Petición POST a https://onep1.herokuapp.com/ranking/rankingMundial

  - JSON: No tiene mensaje 


  - Devuelve: 
    - Si va bien: codigo 200, 
          {
              "message": "["3nsalada2,10","3nsalada,3","usuario123,0","paulapruebas,0","Helios,0","victor,0","3nsalada3,0","nere.g,0","nereapruebas,0","victorg,0"]"
          }
    - Si va mal: codigo 4**, y por qué falla

## Websockets

Endpoint al que se debe conectar el websocket: https://onep1.herokuapp.com/onep1-game



Cada usuario debería suscribirse a /user/{username}/msg (es donde llegarán los mensajes específicos para el usuario)

Suscribirse a una partida /topic/game/{roomId}



Enviar un mensaje para conectarse a la partida /connect/{roomId}
  - Header : nombre de usuario

Enviar un mensaje para empezar una partida /begin/{roomId}
  - Header : nombre de usuario

Enviar un mensaje para desconectarse de una partida /diconnect/{roomId}
  - Header : nombre de usuario

Enviar un mensaje para jugar una carta /card/play/{roomId}
  - Header : nombre de usuario
  - Body :
 
        {
          "num" : [CERO, UNO, DOS, TRES, CUATRO, CINCO, SEIS, SIETE, OCHO, NUEVE, BLOQUEO, MAS_DOS, CAMBIO_SENTIDO, UNDEFINED, MAS_CUATRO],
        "col" : [ROJO, AMARILLO, AZUL, VERDE, CAMBIO_COLOR]
        }
      
Enviar un mensaje para robar n cartas /card/draw/{roomId}
  - Header : nombre de usuario
  - Body :
 
        {
          "nCards" : <numero_cartas_a_robar>
        }
