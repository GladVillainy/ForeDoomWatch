package exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(ApiException.class);

    public EntityNotFoundException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("ApiException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }
}
