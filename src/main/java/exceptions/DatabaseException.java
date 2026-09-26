package exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class DatabaseException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(ApiException.class);

    public DatabaseException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("ApiException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }
}
