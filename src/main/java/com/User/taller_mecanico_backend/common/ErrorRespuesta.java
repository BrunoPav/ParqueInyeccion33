package com.User.taller_mecanico_backend.common;

import java.time.Instant;

public record ErrorRespuesta(int status, String mensaje, Instant timestamp){}
