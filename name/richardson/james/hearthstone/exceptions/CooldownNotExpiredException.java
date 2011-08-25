
package name.richardson.james.hearthstone.exceptions;

public class CooldownNotExpiredException extends Exception {

  private static final long serialVersionUID = -1736290670858467379L;
  private Long cooldownTime;

  public CooldownNotExpiredException(final Long cooldownTime) {
    this.cooldownTime = cooldownTime;
  }

  public Long getCooldownTime() {
    return cooldownTime;
  }

  public void setCooldownTime(final Long cooldownTime) {
    this.cooldownTime = cooldownTime;
  }

}
