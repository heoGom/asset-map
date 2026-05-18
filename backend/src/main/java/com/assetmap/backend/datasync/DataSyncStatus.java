package com.assetmap.backend.datasync;

import com.assetmap.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
		name = "data_sync_status",
		uniqueConstraints = @UniqueConstraint(name = "uk_data_sync_status_key", columnNames = {"sync_type", "source", "target_key"})
)
public class DataSyncStatus extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "sync_type", nullable = false)
	private DataSyncType syncType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DataSyncSource source;

	@Column(name = "target_key", nullable = false)
	private String targetKey;

	private LocalDate lastSuccessDate;
	private LocalDateTime lastSuccessAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DataSyncStatusValue status;

	@Column(length = 1000)
	private String message;

	protected DataSyncStatus() {
	}

	public DataSyncStatus(DataSyncType syncType, DataSyncSource source, String targetKey) {
		this.syncType = syncType;
		this.source = source;
		this.targetKey = targetKey;
		this.status = DataSyncStatusValue.SKIPPED;
	}

	public void markRunning(String message) {
		this.status = DataSyncStatusValue.RUNNING;
		this.message = message;
	}

	public void markSuccess(LocalDate successDate, LocalDateTime successAt, String message) {
		this.lastSuccessDate = successDate;
		this.lastSuccessAt = successAt;
		this.status = DataSyncStatusValue.SUCCESS;
		this.message = message;
	}

	public void markFailed(String message) {
		this.status = DataSyncStatusValue.FAILED;
		this.message = message;
	}

	public void markSkipped(String message) {
		this.status = DataSyncStatusValue.SKIPPED;
		this.message = message;
	}

	public Long getId() { return id; }
	public DataSyncType getSyncType() { return syncType; }
	public DataSyncSource getSource() { return source; }
	public String getTargetKey() { return targetKey; }
	public LocalDate getLastSuccessDate() { return lastSuccessDate; }
	public LocalDateTime getLastSuccessAt() { return lastSuccessAt; }
	public DataSyncStatusValue getStatus() { return status; }
	public String getMessage() { return message; }
}
