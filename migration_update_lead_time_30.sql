-- 将提前预约时间由 60 分钟改为 30 分钟（已有库执行本脚本即可生效）
UPDATE system_config
SET config_value = '30'
WHERE config_key = 'reservation_lead_time_minutes';
