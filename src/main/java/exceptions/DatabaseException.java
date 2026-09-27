package exceptions;

import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseException extends RuntimeException {
    private HttpStatus code;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseException.class);

    public DatabaseException(HttpStatus code, String msg){
        super(msg);
        this.code = code;
        logger.error("DatabaseException (code={}): {}", code, msg);
    }
    public HttpStatus getCode(){
        return code;
    }
}
