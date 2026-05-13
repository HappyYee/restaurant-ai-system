FROM node:20-alpine AS build

WORKDIR /app
ARG VITE_API_BASE_URL=https://api.yee.earth/api
ARG VITE_ENABLE_MOCK_FALLBACK=false
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_ENABLE_MOCK_FALLBACK=$VITE_ENABLE_MOCK_FALLBACK

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM caddy:2-alpine

COPY --from=build /app/dist /srv/admin-web
