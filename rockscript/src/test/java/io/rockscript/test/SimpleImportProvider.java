package io.rockscript.test;

import io.rockscript.activity.*;
import io.rockscript.engine.EngineEndActivityResponse;
import io.rockscript.engine.ScriptService;
import io.rockscript.engine.impl.ContinuationReference;

import java.util.*;

public class SimpleImportProvider extends ImportObject implements ImportProvider {

  protected static Map<String,List<ContinuationReference>> waits;
  
  public static void setUp() {
    waits = new HashMap<>();
  }

  public static EngineEndActivityResponse endWait(String scriptExecutionId, ScriptService scriptService) {
    ContinuationReference continuationReference = removeFirstContinuationReference(scriptExecutionId);
    return scriptService.newEndActivityCommand()
      .scriptExecutionId(scriptExecutionId)
      .continuationReference(continuationReference)
      .execute();
  }

  public static ContinuationReference removeFirstContinuationReference(String scriptExecutionId) {
    checkWaitsInitialized();
    List<ContinuationReference> continuationReferences = waits.get(scriptExecutionId);
    if (continuationReferences==null) throw new RuntimeException("No continuation references for script execution "+scriptExecutionId);
    return continuationReferences.remove(0);
  }

  public SimpleImportProvider() {
    super("rockscript.io/simple");
    put(new AbstractActivity("wait", new String[]{}) {
      @Override
      public ActivityOutput invoke(ActivityInput input) {
        checkWaitsInitialized();
        waits
          .computeIfAbsent(input.getScriptExecutionId(), seid->new ArrayList<>())
          .add(input.getContinuationReference());
        return ActivityOutput.waitForEndActivityCallback();
      }
    });
    put(new AbstractActivity("noop", new String[]{}) {
      @Override
      public ActivityOutput invoke(ActivityInput input) {
        return ActivityOutput.endActivity("noop");
      }
    });
  }

  private static void checkWaitsInitialized() {
    if (waits==null) throw new RuntimeException("In the setUp of your test you have to call SimpleImportProvider.setUp();");
  }

  @Override
  public ImportObject getImportObject() {
    return this;
  }
}
