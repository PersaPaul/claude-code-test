import { LitElement, css, html } from 'lit';
import { repeat } from 'lit/directives/repeat.js';
import './todo-item.js';

/** Presentational list; item events bubble past it to <todo-app>. */
export class TodoList extends LitElement {
  static properties = {
    todos: { type: Array },
    emptyMessage: { type: String },
  };

  constructor() {
    super();
    this.todos = [];
    this.emptyMessage = 'Nothing here yet.';
  }

  render() {
    if (this.todos.length === 0) {
      return html`<p class="empty">${this.emptyMessage}</p>`;
    }
    return html`
      <ul>
        ${repeat(
          this.todos,
          (todo) => todo.id,
          (todo) => html`<li><todo-item .todo=${todo}></todo-item></li>`,
        )}
      </ul>
    `;
  }

  static styles = css`
    ul {
      margin: 0;
      padding: 0;
      list-style: none;
    }
    li + li {
      border-top: 1px solid var(--border);
    }
    .empty {
      margin: 0;
      padding: 28px 14px;
      color: var(--muted);
      text-align: center;
    }
  `;
}

customElements.define('todo-list', TodoList);
