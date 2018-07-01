package zexception;

public class DiffIntervalException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 8711490887808301068L;

  public DiffIntervalException(){
      super();
  }

  public DiffIntervalException(String message){
      super(message);
  }
}
