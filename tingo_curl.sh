#!/bin/bash

# Fetching EOD Data for GOOGL
curl -X GET "https://api.tiingo.com/tiingo/daily/GOOGL/prices" \
-H "Content-Type: application/json" \
-H "Authorization: fc035fd7df94b70cfbd86b6157eb744e8c377c0b"

# Fetching EOD Data for AAPL
curl -X GET "https://api.tiingo.com/tiingo/daily/AAPL/prices" \
-H "Content-Type: application/json" \
-H "Authorization: fc035fd7df94b70cfbd86b6157eb744e8c377c0b"

# Fetching EOD Data for MSFT
curl -X GET "https://api.tiingo.com/tiingo/daily/MSFT/prices" \
-H "Content-Type: application/json" \
-H "Authorization: fc035fd7df94b70cfbd86b6157eb744e8c377c0b"