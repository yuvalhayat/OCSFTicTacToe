package il.cshaifasweng.OCSFMediatorExample.entities;

import java.io.Serializable;
import java.time.LocalTime;

public class Warning implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8224097662914849956L;
	
	private Response response;
	private Object data;

	public Response getResponse() {
		return response;
	}
    public Object getData() {
        return data;
    }

	public Warning(Response response,Object data) {
		this.response = response;
		this.data = data;
	}
}
