package com.cerea_p1.spring.jpa.postgresql.controller;

import com.cerea_p1.spring.jpa.postgresql.model.Usuario;
import com.cerea_p1.spring.jpa.postgresql.payload.request.Profile.DeleteUserRequest;
import com.cerea_p1.spring.jpa.postgresql.payload.response.MessageResponse;
import com.cerea_p1.spring.jpa.postgresql.repository.UsuarioRepository;
import com.cerea_p1.spring.jpa.postgresql.security.jwt.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.logging.*;

@RestController
@CrossOrigin(allowCredentials = "true", origins = "http://localhost:4200/")
@RequestMapping("/user")
public class UserController {
    @Autowired
	UsuarioRepository userRepository;
	private static final Logger logger = Logger.getLogger("MyLog");

	@Autowired
	JwtUtils jwtUtils;

    @PostMapping("/deleteUser")
	public ResponseEntity<?> deleteUser(@RequestBody DeleteUserRequest deleteUserRequest) {
		logger.info("user1=" + deleteUserRequest.getUsername() );
		if ( (!userRepository.existsByUsername(deleteUserRequest.getUsername())) ) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: No se pudo encontrar el usuario"));
		
		}else {
			Optional<Usuario> opUser = userRepository.findByUsername(deleteUserRequest.getUsername());
			if(opUser.isPresent()){
				Usuario u = opUser.get();
                userRepository.delete(u);
                return ResponseEntity.ok(new MessageResponse("Se ha eliminado el usuario correctamente"));
			} else return ResponseEntity.badRequest().body(new MessageResponse("Error: No se puede recuper el usuario."));
		}
	}

}
