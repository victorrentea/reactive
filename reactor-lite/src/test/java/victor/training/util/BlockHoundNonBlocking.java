package victor.training.util;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;

public class BlockHoundNonBlocking implements BeforeEachCallback {
  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    System.out.println("HALO!");
    Object testClassInstance = context.getTestInstance().get();
    for (Field field : testClassInstance.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      System.out.println("Found field " + field.getName() + " = " + field.get(testClassInstance));
    }
  }
}
