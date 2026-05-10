CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    client_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    target_url VARCHAR(512) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE notification_events (
    notification_event_id VARCHAR(64) PRIMARY KEY,
    source_event_id VARCHAR(64) NOT NULL,
    client_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    event_created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    delivery_status VARCHAR(32) NOT NULL,
    attempt_count INTEGER NOT NULL,
    last_attempt_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    http_status INTEGER,
    final_failure_reason VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    delivery_claimed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notification_events_client_created_at
    ON notification_events (client_id, event_created_at);

CREATE INDEX idx_notification_events_client_status_created_at
    ON notification_events (client_id, delivery_status, event_created_at);

CREATE INDEX idx_notification_events_status_next_retry_at
    ON notification_events (delivery_status, next_retry_at);

CREATE INDEX idx_subscriptions_client_event_active
    ON subscriptions (client_id, event_type, active);

