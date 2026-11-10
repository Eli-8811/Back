package com.core.back.payload;
import lombok.Data;
import java.util.List;

@Data
public class AssignPermissionsRequest {
    private List<Long> permisoIds;
}