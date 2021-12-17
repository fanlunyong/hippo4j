package cn.hippo4j.config.monitor;

import cn.hippo4j.common.executor.ExecutorFactory;
import cn.hippo4j.config.model.HisRunDataInfo;
import cn.hippo4j.config.service.biz.HisRunDataService;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.hippo4j.common.constant.Constants.DEFAULT_GROUP;

/**
 * Regularly clean up the historical running data of thread pool.
 *
 * @author chen.ma
 * @date 2021/12/17 20:13
 */
@Component
@RequiredArgsConstructor
public class TimeCleanHistoryDataTask implements Runnable, InitializingBean {

    @Value("${clean.history.data.period:30}")
    private Long cleanHistoryDataPeriod;

    @NonNull
    private final HisRunDataService hisRunDataService;

    private final ScheduledExecutorService cleanHistoryDataExecutor = ExecutorFactory.Managed
            .newSingleScheduledExecutorService(DEFAULT_GROUP, r -> new Thread(r, "clean-history-data"));

    @Override
    public void run() {
        Date currentDate = new Date();
        DateTime offsetMinuteDateTime = DateUtil.offsetMinute(currentDate, (int) -cleanHistoryDataPeriod);

        LambdaQueryWrapper<HisRunDataInfo> queryWrapper = Wrappers.lambdaQuery(HisRunDataInfo.class)
                .le(HisRunDataInfo::getTimestamp, offsetMinuteDateTime.getTime());

        hisRunDataService.remove(queryWrapper);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cleanHistoryDataExecutor.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);
    }

}