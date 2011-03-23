package com.peruzzo.torpedeira;

public class Mensagem {
	
	private int status;
	
	private String numero;
	
	private String mensagem;
	
	

	public Mensagem(int status, String numero, String mensagem) {
		super();
		this.status = status;
		this.numero = numero;
		this.mensagem = mensagem;
	}

	public Mensagem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@Override
	public String toString() {
		return numero + ": " + mensagem;
	}
	
	

}
