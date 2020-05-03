package io.warp10.ext.makair;

import io.warp10.script.NamedWarpScriptFunction;
import io.warp10.script.WarpScriptException;
import io.warp10.script.WarpScriptStack;
import io.warp10.script.WarpScriptStackFunction;

public class MAKAIRTELEMETRYTO extends NamedWarpScriptFunction implements WarpScriptStackFunction {
  public MAKAIRTELEMETRYTO(String name) {
    super(name);
  }
  
  @Override
  public Object apply(WarpScriptStack stack) throws WarpScriptException {
    
    Object top = stack.pop();
    
    if (!(top instanceof byte[])) {
      throw new WarpScriptException(getName() + " operates on a byte array.");
    }
    
    stack.push(MakAirTelemetry.decode((byte[]) top));
    return stack;
  }

}
