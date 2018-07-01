package zexception;

public class DiffAgentException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -731809793907265063L;

  public DiffAgentException(){
      super();
  }

  public DiffAgentException(String message){
      super(message);
  }
}
