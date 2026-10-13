package br.jus.tst.esocialjt.empresa;

import org.springframework.http.HttpStatus;

public class EmpresaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final HttpStatus status;

	public EmpresaException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
