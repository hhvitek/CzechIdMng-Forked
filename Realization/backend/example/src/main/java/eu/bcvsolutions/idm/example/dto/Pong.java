package eu.bcvsolutions.idm.example.dto;

import java.io.Serializable;
import java.util.UUID;

import java.time.ZonedDateTime;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Example ping - pong response dto
 * 
 * @author Radek Tomiška
 *
 */
@Schema(description = "Ping - Pong response")
public class Pong implements BaseDto {
	
	private static final long serialVersionUID = 1L;
	//
	@Schema(required = true, description = "Unique pong identifier")
	private UUID id;
	@Schema(description = "Ping - Pong response message")
	private String message;
	@Schema(required = true, description = "Creation time")
	private ZonedDateTime created;

	@Override
	public Serializable getId() {
		return id;
	}
	
	public Pong() {
		id = UUID.randomUUID();
		created = ZonedDateTime.now(); 
	}
	
	public Pong(String message) {
		this();
		this.message = message;
	}

	@Override
	public void setId(Serializable id) {
		if (id != null) {
			Assert.isInstanceOf(UUID.class, id, "Pong supports only UUID identifier.");
		}
		this.id = (UUID) id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ZonedDateTime getCreated() {
		return created;
	}
	
	public void setCreated(ZonedDateTime created) {
		this.created = created;
	}
}
