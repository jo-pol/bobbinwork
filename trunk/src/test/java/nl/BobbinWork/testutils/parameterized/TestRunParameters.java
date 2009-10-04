package nl.BobbinWork.testutils.parameterized;

import java.util.Arrays;
import org.junit.runners.Parameterized;

public abstract class TestRunParameters
{

  static final String    NEW_LINE = System.getProperty( "line.separator" );
  private final String   description;
  private final Object[] input;

  static interface Builder
  {
    /**
     * Creates an object array for the parameters method of a
     * {@link Parameterized} class.
     * 
     * @param inputParamaters
     *          input arguments for the test run
     * @return a TestRunParameters object wrapped in an object array
     */
    public abstract Object[] withInput(
        Object... inputParamaters);
  }

  TestRunParameters(final String description, final Object[] input)
  {
    /* Not public: disables subclasses outside the package */

    this.description = description;
    this.input = input;
  }

  @Override
  public String toString()
  {
    final StringBuffer returnValue = new StringBuffer();

    returnValue
        .append( NEW_LINE + "input:" + Arrays.deepToString( getInput() ) );
    if (description != null) {
      returnValue.append( NEW_LINE + "tag: " + description );
    }
    returnValue.append( NEW_LINE );
    return returnValue.toString();
  }

  abstract void assertResult(
      final Object[] actual);

  abstract void assertResult(
      final Exception exception);

  Object[] getInput()
  {
    return input;
  }
}