package io.warp10.ext.makair;

import java.util.HashMap;
import java.util.Map;

import io.warp10.warp.sdk.WarpScriptExtension;

public class MakAirWarpScriptExtension extends WarpScriptExtension {
  
  private static Map<String, Object> functions;
  
  static {
    functions = new HashMap<String,Object>();
    
    functions.put("MAKAIR.TELEMETRY->", new MAKAIRTELEMETRYTO("MAKAIR.TELEMETRY->"));
  }
  
  @Override
  public Map<String, Object> getFunctions() {
    return functions;
  }

}
