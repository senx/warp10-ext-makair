package io.warp10.ext.makair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class MakAirTelemetry {
  
  private static enum Mode {
    PRODUCTION,
    QUALIFICATION,
    INTEGRATIONTEST,
  }
  
  private static enum Phase {
    INHALATION,
    EXHALATION,
  }
  
  private static enum SubPhase {
    INSPIRATION,
    HOLD_INSPIRATION,
    EXHALE,
  }
  
  private static enum AlarmPriority {
    LOW,
    MEDIUM,
    HIGH,
  }
  
  /**
   * @see <a href="https://github.com/makers-for-life/makair/blob/master/src/software/telemetry/src/parsers.rs">Telemetry</a>
   * @param packet
   * @return
   */
  public static Map<Object,Object> decode(byte[] packet) {
    
    if (':' != packet[1]) {
      return null;
    }
    
    ByteBuffer bb = ByteBuffer.wrap(packet);
    bb.order(ByteOrder.BIG_ENDIAN);
    
    byte type = bb.get();
    // Skip ':'
    bb.get();
    
    int version_len = 0;
    byte[] version = null;
    String ver = null;
    byte[] devid = null;
    long systick = 0L;
    
    Map<Object,Object> struct = new HashMap<Object, Object>();
    
    switch (type) {
      case 'B': // Boot
        //
        // Header
        //
        Preconditions.checkArgument((byte) 0x01 == bb.get());
        version_len = bb.get();
        version = new byte[version_len];
        bb.get(version);
        ver = new String(version, StandardCharsets.ISO_8859_1);
        // Extract device id
        devid = new byte[12];
        bb.get(devid);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        // Extract systick
        systick = bb.getLong();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        
        // Mode
        Mode mode = Mode.values()[(int) bb.get() - 1];
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());        
        // Value128
        long value128 = (long) bb.get();
        
        struct.put("type", "BootMessage");
        struct.put("version", ver);
        struct.put("device_id", devid);
        struct.put("systick", systick);
        struct.put("mode", mode.name());
        struct.put("value128", value128);
        
        break;
        
      case 'O':
        //
        // Header
        //
        Preconditions.checkArgument((byte) 0x01 == bb.get());        
        version_len = bb.get();
        version = new byte[version_len];
        bb.get(version);
        ver = new String(version, StandardCharsets.ISO_8859_1);
        // Extract device id
        devid = new byte[12];
        bb.get(devid);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        // Extract systick
        systick = bb.getLong();
        
        struct.put("type", "StoppedMessage");
        struct.put("version", ver);
        struct.put("device_id", devid);
        struct.put("systick", systick);

        break;
        
      case 'D':
        //
        // Header
        //
        Preconditions.checkArgument((byte) 0x01 == bb.get());        
        version_len = bb.get();
        version = new byte[version_len];
        bb.get(version);
        ver = new String(version, StandardCharsets.ISO_8859_1);
        // Extract device id
        devid = new byte[12];
        bb.get(devid);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        // Extract systick
        systick = bb.getLong();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());

        long centile = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long pressure = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());        
        int phases = bb.get();
        Phase phase = null;
        SubPhase subphase = null;
        if (0x10 == (phases & 0x10)) {          
          phase = Phase.INHALATION;
        } else if (0x40 == (phases & 0x40)) {
          phase = Phase.EXHALATION;
        } else {
          throw new RuntimeException("Invalid phase.");
        }
        if (0x1 == (phases & 0x1)) {
          subphase = SubPhase.INSPIRATION;
        } else if (0x2 == (phases & 0x2)) {
          subphase = SubPhase.HOLD_INSPIRATION;
        } else if (0x4 == (phases & 0x4)) {
          subphase = SubPhase.EXHALE;
        } else {
          throw new RuntimeException("Invalid subphase");
        }
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long blower_valve_position = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long patient_valve_position = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long blower_rpm = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long battery_level = bb.get();

        struct.put("type", "DataSnapshot");
        struct.put("version", ver);
        struct.put("device_id", devid);
        struct.put("systick", systick);
        struct.put("centile", centile);
        struct.put("pressure", pressure);
        struct.put("phase", phase.name());
        struct.put("subphase", subphase.name());
        struct.put("blower_valve_position", blower_valve_position);
        struct.put("patient_valve_position", patient_valve_position);
        struct.put("blower_rpm", blower_rpm);
        struct.put("battery_level", battery_level);

        break;
        
      case 'S':
        //
        // Header
        //
        Preconditions.checkArgument((byte) 0x01 == bb.get());        
        version_len = bb.get();
        version = new byte[version_len];
        bb.get(version);
        ver = new String(version, StandardCharsets.ISO_8859_1);
        // Extract device id
        devid = new byte[12];
        bb.get(devid);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        // Extract systick
        systick = bb.getLong();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        
        long cycle = bb.getInt();
        System.out.println("CYCLE=" + cycle);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long peak_command = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long plateau_command = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long peep_command = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long cpm_command = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long previous_peak_pressure = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long previous_plateau_pressure = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long previous_peep_pressure = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        int len = bb.get();
        List<Long> current_alarm_codes = new ArrayList<Long>(len);
        for (int i = 0; i < len; i++) {
          current_alarm_codes.add((long) bb.get());
        }
        
        struct.put("type", "MachineStateSnapshot");
        struct.put("version", ver);
        struct.put("device_id", devid);
        struct.put("systick", systick);
        struct.put("cycle", cycle);
        struct.put("peak_command", peak_command);
        struct.put("plateau_command", plateau_command);
        struct.put("peep_command", peep_command);
        struct.put("cpm_command", cpm_command);
        struct.put("previous_peak_pressure", previous_peak_pressure);
        struct.put("previous_plateau_pressure", previous_plateau_pressure);
        struct.put("previous_peep_pressure", previous_peep_pressure);
        struct.put("current_alarm_codes", current_alarm_codes);

        break;      
      case 'T':
        //
        // Header
        //
        Preconditions.checkArgument((byte) 0x01 == bb.get());        
        version_len = bb.get();
        version = new byte[version_len];
        bb.get(version);
        ver = new String(version, StandardCharsets.ISO_8859_1);
        // Extract device id
        devid = new byte[12];
        bb.get(devid);
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        // Extract systick
        systick = bb.getLong();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        centile = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        pressure = bb.getShort();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());        
        phases = bb.get();
        if (0x10 == (phases & 0x10)) {          
          phase = Phase.INHALATION;
        } else if (0x40 == (phases & 0x40)) {
          phase = Phase.EXHALATION;
        } else {
          throw new RuntimeException("Invalid phase.");
        }
        if (0x1 == (phases & 0x1)) {
          subphase = SubPhase.INSPIRATION;
        } else if (0x2 == (phases & 0x2)) {
          subphase = SubPhase.HOLD_INSPIRATION;
        } else if (0x4 == (phases & 0x4)) {
          subphase = SubPhase.EXHALE;
        } else {
          throw new RuntimeException("Invalid subphase");
        }
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        cycle = bb.getInt();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long alarm_code = bb.get();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        int prio = bb.get();
        AlarmPriority alarm_priority = null;
        
        if (0x01 == prio) {
          alarm_priority = AlarmPriority.LOW;
        } else if (0x02 == prio) {
          alarm_priority = AlarmPriority.MEDIUM;          
        } else if (0x04 == prio) {
          alarm_priority = AlarmPriority.HIGH;
        } else {
          throw new RuntimeException("Invalid alarm priority.");
        }
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        byte trig = bb.get();
        boolean triggered = false;
        if (0xF0 == (trig & 0xFF)) {
          triggered = true;
        } else if (0x0F == (trig & 0xFF)) {
          triggered = false;
        } else {
          throw new RuntimeException("Invalid triggered value.");
        }
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long expected = bb.getInt();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long measured = bb.getInt();
        // Skip separator
        Preconditions.checkArgument((byte) 0x09 == bb.get());
        long cycles_since_trigger = bb.getInt();
        
        struct.put("type", "AlarmTrap");
        struct.put("version", ver);
        struct.put("device_id", devid);
        struct.put("systick", systick);
        struct.put("centile", centile);
        struct.put("pressure", pressure);
        struct.put("phase", phase.name());
        struct.put("subphase", subphase.name());
        struct.put("cycle", cycle);
        struct.put("alarm_code", alarm_code);
        struct.put("alarm_priority", alarm_priority.name());
        struct.put("triggered", triggered);
        struct.put("expected", expected);
        struct.put("measured", measured);
        struct.put("cycles_since_trigger", cycles_since_trigger);
        break;
        
      default:
        return null;
    }
    return struct;
  }
}
