package editor.netservice;

import java.io.IOException;

/**
 * Date: 09.02.15
 *
 * @author Vsevolod Kosulnikov
 */
public class BadRequestException extends IOException {
    public BadRequestException(String message) {
        super(message);
    }
}
