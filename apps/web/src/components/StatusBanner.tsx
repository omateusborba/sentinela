interface StatusBannerProps {
  message: string;
  onRetry?: () => void;
}

export function StatusBanner({ message, onRetry }: StatusBannerProps) {
  return (
    <div className="status-banner" role="alert">
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="btn btn--ghost" onClick={onRetry}>
          Tentar novamente
        </button>
      )}
    </div>
  );
}

export function LoadingOverlay() {
  return (
    <div className="loading-overlay" aria-busy="true" aria-label="Carregando dados">
      <div className="loading-overlay__spinner" />
      <p>Consultando satélite…</p>
    </div>
  );
}
