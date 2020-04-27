package eu.sshopencloud.marketplace.mappers.activities;

import eu.sshopencloud.marketplace.dto.activities.ActivityDto;
import eu.sshopencloud.marketplace.model.activities.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ActivityMapper {

    ActivityMapper INSTANCE = Mappers.getMapper(ActivityMapper.class);

    ActivityDto toDto(Activity activity);

    List<Activity> toDto(List<Activity> activities);

}
