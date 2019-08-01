package mostwanted.domain.dtos.races;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RaceImportDto {

    @XmlElement
    private int laps;

    @XmlElement(name = "district-name")
    private String district;

    @XmlElement
    private List<EntryImportRootDto> entries;

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public List<EntryImportRootDto> getEntries() {
        return entries;
    }

    public void setEntries(List<EntryImportRootDto> entries) {
        this.entries = entries;
    }
}
