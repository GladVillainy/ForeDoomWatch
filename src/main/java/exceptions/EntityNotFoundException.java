package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityNotFoundException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(EntityNotFoundException.class);

    public EntityNotFoundException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("EntityNotFoundException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }
}
