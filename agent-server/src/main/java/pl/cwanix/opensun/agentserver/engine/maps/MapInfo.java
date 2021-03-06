package pl.cwanix.opensun.agentserver.engine.maps;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapInfo {

    private int mapCode;
    private int mapKind;
    private String debugName;
    private int nCode;
    private int dCode;
    private int mKind;
    private int mType;
    private int qCode;
    private int minUserNum;
    private int maxUserNum;
    private int minLv;
    private int maxLv;
    private String mapControlId;
    private int text1;
    private int text2;
    private int text3;
}
