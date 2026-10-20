import { Button } from "./Button.tsx";
import { Modal } from "./Modal.tsx";

export function ConfirmDialog({
  open,
  title,
  body,
  confirmLabel,
  busy = false,
  onConfirm,
  onClose,
}: {
  open: boolean;
  title: string;
  body: string;
  confirmLabel: string;
  busy?: boolean;
  onConfirm: () => void;
  onClose: () => void;
}) {
  return (
    <Modal
      open={open}
      title={title}
      description={body}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={busy}>
            Cancel
          </Button>
          <Button variant="danger" onClick={onConfirm} disabled={busy}>
            {busy ? "Removing…" : confirmLabel}
          </Button>
        </>
      }
    />
  );
}
