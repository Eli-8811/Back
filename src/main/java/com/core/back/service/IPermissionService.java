package com.core.back.service;

import com.core.back.payload.PermissionRequest;
import java.util.List;

public interface IPermissionService {
    
    PermissionRequest savePermission(PermissionRequest permissionRequest);
    
}