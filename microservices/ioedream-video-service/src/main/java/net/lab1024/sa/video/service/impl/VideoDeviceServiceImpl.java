package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.video.service.VideoDeviceService;
import net.lab1024.sa.video.dao.VideoDeviceDao;
import net.lab1024.sa.video.domain.entity.VideoDeviceEntity;
import net.lab1024.sa.video.domain.form.VideoDeviceAddForm;
import net.lab1024.sa.video.domain.form.VideoDeviceUpdateForm;
import net.lab1024.sa.video.domain.form.VideoDeviceQueryForm;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.manager.VideoDeviceManager;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.SmartBeanUtil;

import java.util.List;