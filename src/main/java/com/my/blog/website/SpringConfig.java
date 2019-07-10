package com.my.blog.website;

import com.my.blog.website.constant.ProfileConst;
import com.my.blog.website.job.GithubQuartzJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Autowired
    ProfileConst profileConst;

    @Bean
    public JobDetail githubQuartzJob () {
        return JobBuilder.newJob(GithubQuartzJob.class).withIdentity("githubQuartzJob").storeDurably().build();
    }

    @Bean
    public Trigger githubQuartzJobTrigger() {
        ScheduleBuilder builder;
        if (ProfileConst.PROFILE_DEVELOP.equalsIgnoreCase(profileConst.getActive())) {
            builder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(60 * 10)  //设置时间周期单位秒
                .repeatForever();
        } else {
            builder = CronScheduleBuilder.cronSchedule("5 36 4,12,22 * * ?");
        }


        return TriggerBuilder.newTrigger().forJob(githubQuartzJob())
                .withIdentity("githubQuartzJob")
                .withSchedule(builder)
                .build();
    }
}