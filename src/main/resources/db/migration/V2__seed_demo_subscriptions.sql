INSERT INTO subscriptions (id, client_id, event_type, target_url, active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'CLIENT001', 'credit_card_payment', 'https://client001.example.com/webhooks/credit-card-payment', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'CLIENT001', 'debit_card_withdrawal', 'https://client001.example.com/webhooks/debit-card-withdrawal', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('33333333-3333-3333-3333-333333333333', 'CLIENT002', 'credit_transfer', 'https://client002.example.com/webhooks/credit-transfer', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('44444444-4444-4444-4444-444444444444', 'CLIENT002', 'debit_automatic_payment', 'https://client002.example.com/webhooks/debit-automatic-payment', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('55555555-5555-5555-5555-555555555555', 'CLIENT003', 'credit_refund', 'https://client003.example.com/webhooks/credit-refund', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

