package com.my.blog.website;

import com.my.blog.website.job.GithubQuartzJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public JobDetail githubQuartzJob () {
        return JobBuilder.newJob(GithubQuartzJob.class).withIdentity("githubQuartzJob").storeDurably().build();
    }

    @Bean
    public Trigger githubQuartzJobTrigger() {
        CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule("5 6 9,23 * * ?");
//        SimpleScheduleBuilder builder = SimpleScheduleBuilder.simpleSchedule()
//                .withIntervalInSeconds(60 * 10)  //设置时间周期单位秒
//                .repeatForever();
        return TriggerBuilder.newTrigger().forJob(githubQuartzJob())
                .withIdentity("githubQuartzJob")
                .withSchedule(builder)
                .build();
    }
}
