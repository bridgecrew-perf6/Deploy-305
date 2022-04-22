package com.cerea_p1.spring.jpa.postgresql.security.services;

import com.cerea_p1.spring.jpa.postgresql.model.game.*;
import com.cerea_p1.spring.jpa.postgresql.exception.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: e.shakeri
 */

@Service
@AllArgsConstructor
public class GameService {

    private ConcurrentHashMap<String,Partida> almacen_partidas;

    //private final SowService sowService;

    public GameService(){
        almacen_partidas = new ConcurrentHashMap<String,Partida>();
    }

    public Partida crearPartida(Jugador jugador) {
        Partida game = new Partida(true);
        game.setId(UUID.randomUUID().toString());
        game.addJugador(jugador);
        game.setEstado(EstadoPartidaEnum.NEW);
        almacen_partidas.put(game.getId(),game);
        return game;
    }

    public Partida connectToGame(Jugador player, String gameId) {

        if(player != null){
            Optional<Partida> optionalGame;
            if(almacen_partidas.containsKey(gameId))
                optionalGame = Optional.of(almacen_partidas.get(gameId));
            else{ optionalGame = null; throw new GameException("Esa partida no existe"); 
            }

            optionalGame.orElseThrow(() -> new GameException("Game with provided id doesn't exist"));
            Partida game = optionalGame.get();

            if(!game.playerAlreadyIn(player))
                game.addJugador(player);
            return game;
        } else {
            throw new GameException("Jugador no valido");
        }
    }

    public void disconnectFromGame(Jugador player, String gameId){
        Optional<Partida> optionalGame;
        if(almacen_partidas.containsKey(gameId))
            optionalGame = Optional.of(almacen_partidas.get(gameId));
        else { optionalGame = null; throw new GameException("Esa partida no existe");
        }

        optionalGame.orElseThrow(() -> new GameException("Game with provided id doesn't exist"));
            Partida game = optionalGame.get();

        if(player != null) {
            if(game.playerAlreadyIn(player)) {
                game.removePlayer(player);
            } else {
                throw new GameException("Jugador no pertenece a la partida");
            }
        } else {
            throw new GameException("Jugador no valido");
        }
    }



    /*public Game connectToRandomGame(Player player) {
        Optional<Game> optionalGame = gameRepository.findFirstByStatusAndSecondPlayerIsNull(GameStatusEnum.NEW);
        optionalGame.orElseThrow(() ->new GameException("There is no available Game!"));
        Game game = optionalGame.get();
        game.setSecondPlayer(player);
        game.setStatus(GameStatusEnum.IN_PROGRESS);
        gameRepository.save(game);
        return game;
    }

    public Game sow(Sow sow) {
        Optional<Game> optionalGame=gameRepository.findById(sow.getGameId());

        optionalGame.orElseThrow(() ->new GameException("Game with provided id doesn't exist"));
        Game game = optionalGame.get();

        Game gameAfterSow=sowService.sow(game,sow.getPitIndex());
        gameRepository.save(gameAfterSow);

        return gameAfterSow;
    } */
}