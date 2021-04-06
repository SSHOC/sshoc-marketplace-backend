package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.auth.UserRole;


public class UserRoleFormatter extends BaseEnumFormatter<UserRole> {

    private static final String USER_ROLE_NAME = "user-role";

    @Override
    protected UserRole toEnum(String enumValue) {
        return UserRole.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return USER_ROLE_NAME;
    }

}
