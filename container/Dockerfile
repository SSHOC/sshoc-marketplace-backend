FROM gliderlabs/herokuish as builder
COPY . /tmp/app
ARG BUILDPACK_URL
ENV USER=herokuishuser
RUN /bin/herokuish buildpack build

FROM gliderlabs/herokuish
COPY --chown=herokuishuser:herokuishuser --from=builder /app /app
ENV PORT=5000
ENV USER=herokuishuser
EXPOSE 5000
CMD ["/bin/herokuish", "procfile", "start", "web"]
