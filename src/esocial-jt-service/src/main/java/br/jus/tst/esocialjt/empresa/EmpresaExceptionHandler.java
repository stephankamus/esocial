package br.jus.tst.esocialjt.empresa;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = EmpresaException.class)
public class EmpresaExceptionHandler {

	@ExceptionHandler(EmpresaException.class)
	public ResponseEntity<Map<String, String>> tratar(EmpresaException e) {
		return ResponseEntity.status(e.getStatus()).body(Collections.singletonMap("mensagem", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> tratar(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining("; "));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("mensagem", msg));
	}
}
