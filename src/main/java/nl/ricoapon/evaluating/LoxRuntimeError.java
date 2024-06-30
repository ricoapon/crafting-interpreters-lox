package nl.ricoapon.evaluating;

import nl.ricoapon.scanning.Token;

public class LoxRuntimeError extends RuntimeException {
    public final Token token;

    LoxRuntimeError(Token token, String message) {
      super(message);
      this.token = token;
    }
}
