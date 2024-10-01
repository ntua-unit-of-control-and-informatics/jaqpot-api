package org.jaqpot.api.service.util

import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils.isEmpty


class IPUtil {
    companion object {
        fun getIPFromHeader(req: HttpServletRequest): String {
            if (!isEmpty(req.getHeader("X-Real-IP"))) {
                return req.getHeader("X-Real-IP")
            }
            return req.remoteAddr
        }
    }
}

