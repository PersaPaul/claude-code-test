import { LitElement, css, html, nothing } from 'lit';

/**
 * Prev/next pager. Pages are 0-based on the wire and 1-based on screen.
 * Emits `page-change` with the requested 0-based page in `event.detail`.
 */
export class TodoPager extends LitElement {
  static properties = {
    page: { type: Number },
    totalPages: { type: Number },
    totalItems: { type: Number },
    disabled: { type: Boolean },
  };

  constructor() {
    super();
    this.page = 0;
    this.totalPages = 1;
    this.totalItems = 0;
    this.disabled = false;
  }

  render() {
    // A single page needs no controls.
    if (this.totalPages <= 1) return nothing;

    const first = this.page === 0;
    const last = this.page >= this.totalPages - 1;

    return html`
      <nav aria-label="Pagination">
        <button ?disabled=${this.disabled || first} @click=${() => this.#go(this.page - 1)}>
          ‹ Prev
        </button>

        <span aria-live="polite">
          Page ${this.page + 1} of ${this.totalPages}
          <span class="muted">· ${this.totalItems} total</span>
        </span>

        <button ?disabled=${this.disabled || last} @click=${() => this.#go(this.page + 1)}>
          Next ›
        </button>
      </nav>
    `;
  }

  #go(page) {
    this.dispatchEvent(new CustomEvent('page-change', { detail: page }));
  }

  static styles = css`
    nav {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      margin-top: 14px;
      font-size: 14px;
    }
    button {
      padding: 5px 12px;
      font: inherit;
      color: var(--text);
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 999px;
      cursor: pointer;
    }
    button:disabled {
      color: var(--muted);
      cursor: not-allowed;
      opacity: 0.5;
    }
    .muted {
      color: var(--muted);
    }
  `;
}

customElements.define('todo-pager', TodoPager);
