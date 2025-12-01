package net.lab1024.sa.admin.module.smart.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.admin.module.smart.video.service.VideoDeviceService;
import net.lab1024.sa.admin.module.smart.video.dao.VideoDeviceDao;
import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.admin.module.smart.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.admin.module.smart.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.admin.module.smart.video.manager.VideoDeviceManager;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.util.SmartBeanUtil;

import java.util.List;