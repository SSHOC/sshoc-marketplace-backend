package eu.sshopencloud.marketplace.dto.items;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MergeCore {

    private List<String> persistentIdList;

    public int getSize(){
        return persistentIdList.size();
    }

    public String getPersistentId(int i){
        return persistentIdList.get(i);
    }
}
