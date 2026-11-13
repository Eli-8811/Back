package com.core.back.repository;

import com.core.back.entity.PermissionEntity;
import java.util.List;
import java.util.Optional;

public interface IPermissionRepository {

    PermissionEntity savePermission(PermissionEntity permission);

}