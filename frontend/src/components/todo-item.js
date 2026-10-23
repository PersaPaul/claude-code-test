import { LitElement, css, html } from 'lit';

/**
 * One row. Emits `todo-toggle`, `todo-delete` and `todo-rename`, each with
 * `detail.id` (events cross a shadow boundary, so the id travels in the detail
 * rather than being read off event.target). This component owns no data.
 */
export class TodoItem extends LitElement {
  static properties = {
    todo: { type: Object },
    _editing: { state: true },
  };

  constructor() {
    super();
    this._editing = false;
  }

  render() {
    const { title, completed } = this.todo;
    return html`
      <div class="row ${completed ? 'done' : ''}">
        <input
          type="checkbox"
          .checked=${completed}
          @change=${() => this.#emit('todo-toggle', { completed: !completed })}
          aria-label=${`Mark "${title}" as ${completed ? 'active' : 'done'}`}
        />
        ${this._editing
          ? html`<input
              class="edit"
              .value=${title}
              @keydown=${this.#onEditKey}
              @blur=${this.#commit}
              aria-label="Edit title"
              maxlength="200"
            />`
          : html`<span class="title" @dblclick=${() => (this._editing = true)} title="Double-click to edit">
              ${title}
            </span>`}
        <button class="delete" @click=${() => this.#emit('todo-delete')} aria-label=${`Delete "${title}"`}>
          ✕
        </button>
      </div>
    `;
  }

  updated(changed) {
    if (changed.has('_editing') && this._editing) {
      const input = this.renderRoot.querySelector('.edit');
      input?.focus();
      input?.select();
    }
  }

  #onEditKey(event) {
    if (event.key === 'Enter') event.target.blur();
    if (event.key === 'Escape') this._editing = false;
  }

  #commit(event) {
    if (!this._editing) return;
    this._editing = false;
    const title = event.target.value.trim();
    if (title && title !== this.todo.title) {
      this.#emit('todo-rename', { title });
    }
  }

  #emit(name, detail = {}) {
    this.dispatchEvent(
      new CustomEvent(name, {
        detail: { id: this.todo.id, ...detail },
        bubbles: true,
        composed: true,
      }),
    );
  }

  static styles = css`
    .row {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 14px;
    }
    .title {
      flex: 1;
      cursor: text;
      overflow-wrap: anywhere;
    }
    .done .title {
      color: var(--muted);
      text-decoration: line-through;
    }
    input[type='checkbox'] {
      flex: none;
      width: 18px;
      height: 18px;
      accent-color: var(--accent);
      cursor: pointer;
    }
    .edit {
      flex: 1;
      padding: 4px 6px;
      font: inherit;
      color: var(--text);
      background: transparent;
      border: 1px solid var(--accent);
      border-radius: 6px;
    }
    .delete {
      flex: none;
      padding: 4px 8px;
      font-size: 14px;
      color: var(--muted);
      background: none;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      opacity: 0;
      transition: opacity 120ms;
    }
    .row:hover .delete,
    .delete:focus-visible {
      opacity: 1;
      color: var(--danger);
    }
    @media (hover: none) {
      .delete {
        opacity: 1;
      }
    }
  `;
}

customElements.define('todo-item', TodoItem);
