package eu.sshopencloud.marketplace.dto.items;

import java.util.List;

public class MergeCore {

    List<String> persistentIdList;

    public int getSize(){
        return persistentIdList.size();
    }

    public String getPersistentId(int i){
        return persistentIdList.get(i);
    }
}
