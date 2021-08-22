package com.parrer;// This file is auto-generated, don't edit it. Thanks.

import com.aliyun.alidns20150109.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.parrer.util.AssertUtil;
import com.parrer.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Sample {

    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.alidns20150109.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dns.aliyuncs.com";
        return new com.aliyun.alidns20150109.Client(config);
    }

    public static void main(String[] args_) throws Exception {
        log.info("开始执行ddns更新...");
        try {
            java.util.List<String> args = java.util.Arrays.asList(args_);
            com.aliyun.alidns20150109.Client client = Sample.createClient("LTAI5t8tX5gtattfRY8Ccz7K", "BaxOZANxCyjNRIblhqG7f4Qi8q3t0L");

            //get record info
            DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = new DescribeSubDomainRecordsRequest()
                    .setSubDomain("lv6.tuanbaol.com")
                    .setDomainName("tuanbaol.com")
                    .setType("AAAA");
            // 复制代码运行请自行打印 API 的返回值
            DescribeSubDomainRecordsResponse describeSubDomainRecordsResponse = client.describeSubDomainRecords(describeSubDomainRecordsRequest);
            log.info("describeSubDomainRecordsResponse-{}", JsonUtil.toString(describeSubDomainRecordsResponse));
            Long totalCount = describeSubDomainRecordsResponse.getBody().totalCount;
            AssertUtil.isTrue(totalCount > 0, "获取解析信息为空！");
            List<DescribeSubDomainRecordsResponseBody.DescribeSubDomainRecordsResponseBodyDomainRecordsRecord> records = describeSubDomainRecordsResponse.getBody().domainRecords.getRecord();
            DescribeSubDomainRecordsResponseBody.DescribeSubDomainRecordsResponseBodyDomainRecordsRecord record = records.get(0);
            AssertUtil.notNull(record, "获取解析记录为空！");

            //get ip value
//            String[] param = {"ifconfig |grep -A7 'enp2s0'|grep 'prefixlen 64'|grep -v 'fe80'|xargs echo|cut -b 7-45"};
            String[] param = {"sh /root/ddns/getip.sh"};
            Process process = Runtime.getRuntime().exec(param[0]);
            boolean exec = process.waitFor(5L, TimeUnit.SECONDS);
            AssertUtil.isTrue(exec, "执行shell命令失败！");
            InputStream errorStream = process.getErrorStream();
            String execErr = IOUtils.toString(errorStream, StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(execErr)) {
                log.error("exec error-{}", execErr);
            }
            InputStream inputStream = process.getInputStream();
            String execRes = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            log.info("execRes addr from linux-{}", execRes);
            String ip = StringUtils.strip(execRes);
            AssertUtil.notEmpty(execRes, "获取ip地址为空！");
            if (StringUtils.equals(record.getValue(), ip)) {
                log.info("服务端和本地相同ip，不更新！");
                return;
            }
            //update record
            UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest()
                    .setRecordId(record.getRecordId())
                    .setRR(record.getRR())
                    .setType(record.getType())
                    .setValue(ip);
            // 复制代码运行请自行打印 API 的返回值
            UpdateDomainRecordResponse updateDomainRecordResponse = client.updateDomainRecord(updateDomainRecordRequest);
            log.info("updateDomainRecordResponse-{}", JsonUtil.toString(updateDomainRecordResponse));
        } catch (Exception e) {
            log.error("error occurred!", e);
        }
        log.info("ddns更新执行结束！");
    }
}
