import { LitElement, css, html } from 'lit';

/** Fires `todo-add` with the trimmed title in `event.detail`. */
export class TodoForm extends LitElement {
  static properties = {
    disabled: { type: Boolean },
    _value: { state: true },
  };

  constructor() {
    super();
    this.disabled = false;
    this._value = '';
  }

  render() {
    return html`
      <form @submit=${this.#submit}>
        <input
          .value=${this._value}
          @input=${(e) => (this._value = e.target.value)}
          placeholder="What needs doing?"
          aria-label="New todo"
          maxlength="200"
          autocomplete="off"
          ?disabled=${this.disabled}
        />
        <button type="submit" ?disabled=${this.disabled || !this._value.trim()}>
          Add
        </button>
      </form>
    `;
  }

  #submit(event) {
    event.preventDefault();
    const title = this._value.trim();
    if (!title) return;
    this.dispatchEvent(new CustomEvent('todo-add', { detail: title }));
    this._value = '';
  }

  static styles = css`
    form {
      display: flex;
      gap: 8px;
    }
    input {
      flex: 1;
      padding: 11px 14px;
      font: inherit;
      color: var(--text);
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
    }
    input:focus-visible {
      outline: 2px solid var(--accent);
      outline-offset: 1px;
    }
    button {
      padding: 11px 18px;
      font: inherit;
      font-weight: 600;
      color: #fff;
      background: var(--accent);
      border: none;
      border-radius: var(--radius);
      cursor: pointer;
    }
    button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `;
}

customElements.define('todo-form', TodoForm);
