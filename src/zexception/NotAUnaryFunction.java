package zexception;

public class NotAUnaryFunction extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -731809793907265063L;

  public NotAUnaryFunction(){
      super();
  }

  public NotAUnaryFunction(String message){
      super(message);
  }
}